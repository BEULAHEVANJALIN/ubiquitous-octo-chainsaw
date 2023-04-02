package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import java.util.Set;
import java.util.HashSet;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull
	@Override
	public Model build(GameSetup setup,
					   Player mrX,
					   ImmutableList<Player> detectives) {
		// TODO
		return new GameModel(setup, mrX, detectives);
	}
	class GameModel implements Model {
		private Board.GameState board;
		private Set<Observer> observers = new HashSet<>();
		private MyGameStateFactory gameStateFactory = new MyGameStateFactory();
		GameModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives ) {
			board = gameStateFactory.build(setup, mrX, detectives);
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return board;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observer == null) throw new NullPointerException();
			if (observers.contains(observer)) throw new IllegalArgumentException();
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if (observer == null) throw new NullPointerException();
			if (!observers.contains(observer)) throw new IllegalArgumentException();
			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			board = board.advance(move);
			Observer.Event event;
			if (board.getWinner().isEmpty()) {
				event = Observer.Event.MOVE_MADE;
			} else {
				event = Observer.Event.GAME_OVER;
			}
			observers.stream()
					.forEach(
							o -> o.onModelChanged(board, event)
					);
		}
	}
}

